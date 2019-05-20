package Runtime.Machine;

import Runtime.JIT.API.DataType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Encapsulates variable storage array
 * @author Joshua Boley
 */
public class StaticMemory
{
    private static StaticMemory storage = null;
    
    /**
     * Initializes variable storage. Must be called before variable storage can
     * be used.
     */
    public static void initialize()
    {
        storage = new StaticMemory();
    }
    
    /**
     * Allocates storage for the given data type.
     * @param type  Type of the data to be stored
     * @return      Object's relative storage location
     */
    public static int allocate(DataType type)
    {
        return storage.allocateStorage(type);
    }
    
    /**
     * Assigns the provided data object of the indicated type to the relative
     * position indicated by the offset.
     * @param dataObj   Data object to store
     * @param type      Type of the data object
     * @param offset    Relative storage location
     */
    public static void assign(Object dataObj, DataType type, int offset)
    {
        storage.store(dataObj, type, offset);
    }
    
    public static Object retrieve(DataType type, int offset)
    {
        return storage.fetch(type, offset);
    }

    private final Map<String, ByteStorCodec> m_codecMap;
    private int m_nextAddr;
    private final List<Byte> m_varstore;
    
    private StaticMemory()
    {
        m_codecMap = new HashMap<>();
        m_nextAddr = 0;
        m_varstore = new ArrayList<>();
        
        initCodecMap();
    }

    private void initCodecMap()
    {
        ByteStorCodec codec;
        
        // Create 32-bit integer codec
        codec = new ByteStorCodec(
            DataType.Int4,
            (Object dataObj, ByteBuffer stream) -> {
                        stream.putInt((int) dataObj);
                        byte[] byteSequence = new byte[DataType.Int4.size()];
                        stream.rewind();
                        stream.get(byteSequence);
                        return byteSequence;
            },
            (ByteBuffer stream) -> stream.getInt()
        );
        m_codecMap.put(DataType.Int4.toString(), codec);
    }
        
    private int allocateStorage(DataType type)
    {
        for (int i = 0; i < type.size(); ++i)
            m_varstore.add((byte) 0x0);
        int offset = m_nextAddr;
        m_nextAddr += type.size();
        return offset;
    }
    
    private void store (Object dataObj, DataType type, int offset)
    {
        ByteStorCodec codec = m_codecMap.get(type.toString());
        codec.encode(dataObj, offset, m_varstore);
    }
    
    private Object fetch (DataType type, int offset)
    {
        ByteStorCodec codec = m_codecMap.get(type.toString());
        return codec.decode(m_varstore, offset);
    }
}

class ByteStorCodec
{
    private final DataType m_dataType;
    private final ByteBuffer m_buff;
    private final BiFunction<Object, ByteBuffer, byte[]> m_encoder;
    private final Function<ByteBuffer, Object> m_decoder;

    ByteStorCodec(
        DataType type,
        BiFunction<Object, ByteBuffer, byte[]> encoderFunc,
        Function<ByteBuffer, Object> decoderFunc
        )
    {
        m_dataType = type;
        m_buff = ByteBuffer.allocate(type.size());
        m_encoder = encoderFunc;
        m_decoder = decoderFunc;
    }
    
    void encode(Object dataObj, int offset, List<Byte> variableStorage)
    {
        // Encode data object as byte sequence
        m_buff.clear();
        byte[] byteSequence = m_encoder.apply(dataObj, m_buff);
        
        // Place byte sequence in storage
        for (int i = 0, j = offset; i < byteSequence.length; ++i, ++j)
            variableStorage.set(j, byteSequence[i]);
    }
    
    Object decode(List<Byte> variableStorage, int offset)
    {
        // Retrieve byte sequence from storage
        byte[] byteSequence = new byte[m_dataType.size()];
        for (int i = 0, j = offset; i < m_dataType.size(); ++i, ++j)
            byteSequence[i] = variableStorage.get(j);
        
        // Buffer and decode
        m_buff.clear();
        m_buff.put(byteSequence);
        m_buff.rewind();
        return m_decoder.apply(m_buff);
    }
    
    DataType getSupportedType()
    {
        return m_dataType;
    }
}
