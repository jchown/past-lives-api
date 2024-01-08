package com.datasmelter.pastlives;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import org.apache.commons.codec.digest.MurmurHash3;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;

record DeadPerson(String id, int born)
{
}

class DeadPeople extends HashMap<String, List<DeadPerson>>
{
}

public class Handler implements RequestHandler<PersonRequest, PersonResponse>
{
    private final DeadPeople deadPeople;
    private final PersonResponse NoOne = new PersonResponse(0, 0, "");
    private final Charset utf8 = StandardCharsets.UTF_8;

    public Handler()
    {
        Sentry.init(options -> {
            options.setDsn("https://33855b057b2346e7f8a8b51d0ef11c98@o4506172979806208.ingest.sentry.io/4506490865188864");
            options.setTracesSampleRate(1.0);
        });

        var mapper = new ObjectMapper();

        try (var zip = new ZipFile("dead-people.zip"))
        {
            var entry = zip.getEntry("dead-people-sorted.json");
            try (var stream = zip.getInputStream(entry))
            {
                var bytes = stream.readAllBytes();
                var json = new String(bytes, utf8);
                deadPeople = mapper.readValue(json, DeadPeople.class);

                System.out.println("Loaded " + deadPeople.size() + " dates of death");

                var first = deadPeople.keySet().iterator().next();
                var died = deadPeople.get(first);

                System.out.println("e.g. " + died.size() + " people died on " + first + ": " + died);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PersonResponse handleRequest(PersonRequest event, Context context)
    {
        System.out.println("Received: " + event);

        int date = event.date();
        boolean found = false;
        for (int i = 1; i <= 7 ; i++)
        {
            if (deadPeople.containsKey(Integer.toString(date - i)))
            {
                date = date - i;
                found = true;
                break;
            }
        }

        if (!found)
            return NoOne;

        var people = deadPeople.get(Integer.toString(date));

        System.out.println("Found " + people.size() + " people");

        int hash = MurmurHash3.hash32x86(event.name().getBytes(utf8)) & 0x7fff_ffff;

        var person = event.famous() ? getFamous(people, hash) : getRandom(people, hash);

        return new PersonResponse(person.born(), date, person.id());
    }

    private DeadPerson getRandom(List<DeadPerson> people, int hash)
    {
        return people.get(hash % people.size());
    }

    private DeadPerson getFamous(List<DeadPerson> people, int hash)
    {
        for (DeadPerson person : people)
        {
            if ((hash & 1) == 0)
                return person;

            hash = hash >> 1;
        }

        return people.get(0);
    }
}
