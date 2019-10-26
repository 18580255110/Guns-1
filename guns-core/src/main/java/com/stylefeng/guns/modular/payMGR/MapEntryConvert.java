package com.stylefeng.guns.modular.payMGR;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/19 14:07
 * @Version 1.0
 */
public class MapEntryConvert implements Converter {
    private static final Logger log = LoggerFactory.getLogger(MapEntryConvert.class);

    private static final String CDATA_PREFIX = "<![CDATA[";
    private static final String CDATA_SUFFIX = "]]>";
    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        AbstractMap map = (AbstractMap) value;
        for (Object obj : map.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            writer.startNode(entry.getKey().toString());
            Object entryValue = entry.getValue();
            if (null == entryValue) {
                log.warn("The key " + entry.getKey()+ "Got null value");
                continue;
            }
            writer.setValue(CDATA_PREFIX + entry.getValue().toString() + CDATA_SUFFIX);
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        Map<String, String> map = new HashMap<String, String>();
        while(reader.hasMoreChildren()) {
            reader.moveDown();
            String value = reader.getValue();
            if (value.startsWith(CDATA_PREFIX)){
                value = value.substring(CDATA_PREFIX.length(), value.indexOf(CDATA_SUFFIX));
            }
            map.put(reader.getNodeName(), value);
            reader.moveUp();
        }
        return map;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return AbstractMap.class.isAssignableFrom(aClass);
    }
}
