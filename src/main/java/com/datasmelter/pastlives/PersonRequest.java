package com.datasmelter.pastlives;

public record PersonRequest(int date, String name, boolean famous)
{
    public boolean isPing()
    {
        return date == 0 && name.equals("PING");
    }
}
