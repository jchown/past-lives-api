package com.datasmelter.pastlives;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.MurmurHash3;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

record PersonRequest(int dateOfBirth, String name)
{
}

record PersonResponse(int dateOfBirth, int dateOfDeath, String id)
{
}

record Person(String id, int born, int died)
{
}

class DeadPeople extends HashMap<String, List<Person>>
{
}

public class Handler implements RequestHandler<PersonRequest, PersonResponse>
{
    private final DeadPeople deadPeople;
    private final PersonResponse NoOne = new PersonResponse(0, 0, "");
    private final ObjectMapper mapper = new ObjectMapper();
    private final Charset utf8 = Charset.forName("UTF-8");

    public Handler()
    {
        try (var zip = new ZipFile("dead-people.zip"))
        {
            var entry = zip.getEntry("dead-people.json");
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

        int date = event.dateOfBirth();
        boolean found = false;
        for (int i = 1; i < 30 ; i++)
        {
            if (deadPeople.containsKey(Integer.toString(date + i)))
            {
                date = date + i;
                found = true;
                break;
            }
        }

        if (!found)
            return NoOne;

        var people = deadPeople.get(Integer.toString(date));

        System.out.println("Found " + people.size() + " people");

        int hash = MurmurHash3.hash32x86(event.name().getBytes(utf8)) & 0x7fff_ffff;

        var person = people.get(hash % people.size());

        return new PersonResponse(person.born(), person.died(), person.id());
    }
}
