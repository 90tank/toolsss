package com.yili.timemachine.sqlgenerator.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TableModel {

    String tableName;
    String tableComment;
    private List<String> fieldList;
    private List<String> commentList;

    public TableModel(String tableName, String tableComment, List<String> fieldList, List<String> commentList) {
        this.tableName = tableName;
        this.tableComment = tableComment;
        this.fieldList = fieldList;
        this.commentList = commentList;
    }
}
