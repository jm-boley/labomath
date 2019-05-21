package Runtime;

/**
 * Stores information about an IO channel, for use in client channel mapping.
 * Information includes a unique channel-mapping identifier (CMID), a source
 * type (if an input channel), and a target type (if an output channel) as
 * detailed below.
 * 
 * SourceType:
 *  Available types:
 *      console         A local console command line
 *      editor          A local script editor
 *      file            A file from a locally mounted partition
 *      remote_console  A remote console command line over socket connection
 *      remote_editor   A remote script editor over socket connection
 *      remote_file     A remote file transferred over socket connection
 *      none            Not an input channel
 * 
 * SinkType:
 *  Available types:
 *      console         A local console
 *      gui             A local GUI component
 *      remote_console  A remote console over socket connection
 *      remote_gui      A remote GUI component over socket connection
 *      none            Not an output channel
 * 
 * @author Joshua Boley
 */
class IONode
{
    enum SourceType
    {
        console,
        editor,
        file,
        remote_console,
        remote_editor,
        remote_file,
        none
    }
    enum SinkType
    {
        console,
        gui,
        remote_console,
        remote_gui,
        none
    }
    
    private final int m_cmid;
    private final SourceType m_source;
    private final SinkType m_target;

    IONode(int cmid, SourceType source, SinkType target)
    {
        m_cmid = cmid;
        m_source = source;
        m_target = target;
    }
    int getCMID()
    {
        return m_cmid;
    }    
    SourceType getSource()
    {
        return m_source;
    }
    SinkType getTarget()
    {
        return m_target;
    }
}
