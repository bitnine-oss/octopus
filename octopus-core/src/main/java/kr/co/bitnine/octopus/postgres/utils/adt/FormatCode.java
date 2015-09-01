package kr.co.bitnine.octopus.postgres.utils.adt;

public enum FormatCode
{
    TEXT(0),
    BINARY(1);

    private final int code;

    FormatCode(int code)
    {
        this.code = code;
    }

    public int code()
    {
        return code;
    }

    public static FormatCode ofCode(int code)
    {
        return code == 0 ? TEXT : BINARY;
    }
}
