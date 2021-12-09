package com.javaoffers.base.batis.core;

import com.javaoffers.base.batis.convert.Convert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @Auther: create by cmj on 2021/12/7 20:12
 */
public class GenericConvertSelector implements ConvertSelector{


    Map<String, Convert> registers = new ConcurrentHashMap<>();

    @Override
    public void registerConvert(Descriptor descriptor, Convert convert) {
        registers.put(descriptor.getUniqueMark(),convert);
    }


    @Override
    public Convert selector(Descriptor descriptor) {
        return registers.get(descriptor.getUniqueMark());
    }
}
