package com.javaoffers.batis.modelhelper.fun.crud.impl.delete;

import com.javaoffers.batis.modelhelper.core.CrudMapperMethodThreadLocal;
import com.javaoffers.batis.modelhelper.core.LinkedConditions;
import com.javaoffers.batis.modelhelper.fun.Condition;
import com.javaoffers.batis.modelhelper.fun.GetterFun;
import com.javaoffers.batis.modelhelper.fun.JdbcTemplateCondition;
import com.javaoffers.batis.modelhelper.fun.condition.DeleteFromCondition;
import com.javaoffers.batis.modelhelper.fun.crud.delete.DeleteFun;
import com.javaoffers.batis.modelhelper.fun.crud.delete.DeleteWhereFun;

public class DeleteFunImpl<M> implements DeleteFun<M, GetterFun<M,Object>,Object> {

    private LinkedConditions<Condition> conditions = new LinkedConditions<>();

    private Class modelClass;


    public DeleteFunImpl(Class modelClass) {
        this.modelClass = modelClass;
        this.conditions.add(new JdbcTemplateCondition(CrudMapperMethodThreadLocal.getExcutorJdbcTemplate()));
        this.conditions.add(new DeleteFromCondition(modelClass));
    }

    @Override
    public DeleteWhereFun<M, GetterFun<M, Object>, Object> where() {
        return new DeleteWhereFunImpl(conditions,this.modelClass);
    }
}
