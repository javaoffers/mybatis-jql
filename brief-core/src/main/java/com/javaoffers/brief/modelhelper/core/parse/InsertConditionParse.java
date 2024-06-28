package com.javaoffers.brief.modelhelper.core.parse;

import com.javaoffers.brief.modelhelper.core.MoreSQLInfo;
import com.javaoffers.brief.modelhelper.core.SQLStatement;
import com.javaoffers.brief.modelhelper.fun.Condition;
import com.javaoffers.brief.modelhelper.fun.ConditionTag;
import com.javaoffers.brief.modelhelper.fun.condition.ColValueCondition;
import com.javaoffers.brief.modelhelper.fun.condition.insert.InsertAllColValueCondition;
import com.javaoffers.brief.modelhelper.fun.condition.insert.InsertIntoCondition;
import com.javaoffers.brief.modelhelper.fun.condition.mark.OnDuplicateKeyUpdateMark;
import com.javaoffers.brief.modelhelper.fun.condition.mark.ReplaceIntoMark;
import com.javaoffers.brief.modelhelper.utils.Assert;

import java.util.*;

/**
 * @description:
 * @author: create by cmj on 2022/10/23 10:44
 */
public class InsertConditionParse extends AbstractParseCondition {

    public static ConditionTag conditionTag  = ConditionTag.INSERT_INTO;

    @Override
    public SQLStatement doParse(LinkedList<Condition> conditions) {
        return parseInsert(conditions);
    }

    private SQLStatement parseInsert(LinkedList<Condition> conditions) {
        InsertIntoCondition insertIntoTableCondition = (InsertIntoCondition)conditions.pollFirst();

        String insertIntoTableSql = insertIntoTableCondition.getSql();

        StringBuilder insertColNamesAppender = new StringBuilder();
        ArrayList<String> moreSql = new ArrayList<>();
        StringBuilder insertValueAppender = new StringBuilder();
        ArrayList<Map<String, Object>> paramsList = new ArrayList<>();
        HashMap<String, Object> valuesParam = new HashMap<>();
        boolean isColValueCondition = false;
        List<String> dupUpdateSql = new ArrayList<>();
        StringBuilder duplicateSqlForColValCondition =
                new StringBuilder();
        boolean isDupUpdateSql = false;
        Condition condition = null;
        while( (condition = conditions.pollFirst()) != null){
            if(condition instanceof ColValueCondition){
                Map<String, Object> params = condition.getParams();//只有一个值
                Assert.isTrue(params.size() == 1,"必须存在一个值");
                if(insertColNamesAppender.length() == 0){
                    isColValueCondition = true;
                    //insertColNamesAppender.append(insertIntoTableSql);
                    insertColNamesAppender.append("(");
                    insertValueAppender.append(ConditionTag.VALUES.getTag());
                    insertValueAppender.append("(");
                    duplicateSqlForColValCondition.append(ConditionTag.ON_DUPLICATE_KEY_UPDATE.getTag());

                }else{
                    insertColNamesAppender.append(",");
                    insertValueAppender.append(",");
                    duplicateSqlForColValCondition.append(",");
                }
                insertColNamesAppender.append(condition.getSql());
                Set<String> strings = params.keySet();
                String key = strings.iterator().next();
                insertValueAppender.append("#{");
                insertValueAppender.append(key);
                insertValueAppender.append("}");
                valuesParam.put(key,params.get(key));
                duplicateSqlForColValCondition.append(condition.getSql());
                duplicateSqlForColValCondition.append(" = ");
                duplicateSqlForColValCondition.append("values(");
                duplicateSqlForColValCondition.append("#{");
                duplicateSqlForColValCondition.append(key);
                duplicateSqlForColValCondition.append("}");
                duplicateSqlForColValCondition.append(") ");
            } else if(condition instanceof InsertAllColValueCondition){
                insertValueAppender = new StringBuilder();
                insertColNamesAppender = new StringBuilder();
                InsertAllColValueCondition allColValueCondition = (InsertAllColValueCondition) condition;
                allColValueCondition.init();

                //insertColNamesAppender.append(insertIntoTableSql);
                insertColNamesAppender.append(allColValueCondition.getSql());
                insertValueAppender.append(ConditionTag.VALUES.getTag());
                insertValueAppender.append(allColValueCondition.getValuesSql());

                paramsList.add(allColValueCondition.getParams());
                moreSql.add(insertColNamesAppender.append(insertValueAppender.toString()).toString());
                dupUpdateSql.add(allColValueCondition.getOnDuplicate());
            }else if(condition instanceof OnDuplicateKeyUpdateMark){
                isDupUpdateSql = true;
                if(duplicateSqlForColValCondition.length() > 0){
                    dupUpdateSql.add(duplicateSqlForColValCondition.toString());
                }
            }else if(condition instanceof ReplaceIntoMark){
                ReplaceIntoMark replaceIntoMark = (ReplaceIntoMark) condition;
                insertIntoTableSql = replaceIntoMark.getSql() + insertIntoTableCondition.getTableName();
            }
        }

        if(isColValueCondition){
            insertColNamesAppender.append(")");
            insertValueAppender.append(")");
            paramsList.add(valuesParam);
            moreSql.add(insertColNamesAppender.append(insertValueAppender.toString()).toString());
        }
        Assert.isTrue(moreSql.size() == paramsList.size()," data asymmetry ");
        MoreSQLInfo moreSQLInfo = new MoreSQLInfo();
        HashMap<String, SQLStatement> batch = new HashMap<>();
        for(int i =0; i < moreSql.size(); i++){
            String sql = insertIntoTableSql + moreSql.get(i);
            if(isDupUpdateSql){
                sql = sql + dupUpdateSql.get(i);
            }
            Map<String, Object> sqlParam = paramsList.get(i);
            SQLStatement sqlStatement = batch.get(sql);
            if(sqlStatement == null){
                ArrayList parems = new ArrayList<>();
                parems.add(sqlParam);
                sqlStatement = SQLStatement.builder()
                        .aClass(insertIntoTableCondition.getModelClass())
                        .params(parems)
                        .sql(sql)
                        .status(true)
                        .build();
                batch.put(sql, sqlStatement);
            }else{
                sqlStatement.getParams().add(sqlParam);
            }

        }
        moreSQLInfo.addAllSqlInfo(batch.values());
        return moreSQLInfo;
    }


}
