package CSCI502.Project.Runtime.Interface;

/**
 *
 * @author Joshua Boley
 */

enum TypeCode
{
    EMPTY(0),
    CHAR(1),
    BOOL(2),
    INT4(3);
    
    private final int m_code;
    private TypeCode (int code) { m_code = code; }
    
    int toInteger() { return m_code; }
}

public enum DataType
{
    // NOTE: Sizes required to convert to byte strings in variable storage segment
    Empty(0, "void"/*, TypeCode.EMPTY*/),       // Empty data type
    Char(1, "char"/*, TypeCode.CHAR*/),     // Character data type
    Bool(2, "bool"/*, TypeCode.BOOL*/),     // 2-byte Boolean data type
    Int4(4, "int4"/*, TypeCode.INT4*/),     // 4-byte integer data type
    Imm_Bool(2, "imm_bool"),                // Boolean immediate (constant literal)
    Imm_Int4(4, "imm_int4"),                // Integer immediate (constant literal)
    Imm_Str(-1, "imm_string"),              // String immediate (constant literal) of indeterminate size
    Register(8, "reg");

    public static DataType getType (String tname)
    {
        switch (tname) {
            case "char":
                return DataType.Char;
            case "bool":
                return DataType.Bool;
            case "int4":
                return DataType.Int4;
            default:
                break;
        }
        return DataType.Empty;
    }

    @Override
    public String  toString()    { return m_name; }
    public int     size()        { return m_size; }
    public boolean equals(DataType rhs)
              { return m_name.equals(rhs.m_name); }

    private final int m_size;           // Size (bytes) of datatype
//    private final TypeCode m_typeCode;  // Type code (for fast lookup)
    private final String m_name;        // Data type string identifier

    private DataType(int size, String name /*, TypeCode typeCode*/)
    {
        m_size = size;
//        m_typeCode = typeCode;
        m_name = name;
    }
}
