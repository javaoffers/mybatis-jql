package com.javaoffers.base.batis.core;

import com.javaoffers.base.batis.convert.Convert;

/**
 * @Description:
 * @Auther: create by cmj on 2021/12/7 20:08
 */
public interface ConvertSelector extends Selector<Convert,Descriptor> {
    /**
     * 注册选择器
     * @param descriptor
     */

    public void registerConvert(Descriptor descriptor, Convert convert);
}
