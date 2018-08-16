package com.tracyis.builder;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.awt.Color;

import javax.swing.JFormattedTextField;

/**
 * Created by Tracy on 2018/8/14.
 */
public class UrlAction extends AnAction {

    private String className;
    private String[] fieldVars;

    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取Editor和Project对象
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);

        if (editor == null || project == null)
            return;

        //获取SelectionModel和Document对象
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();

        //得到选中字符串的起始和结束位置
        int endOffset = selectionModel.getSelectionEnd();
        //得到最大插入字符串位置
        int maxOffset = document.getTextLength() - 1;
        //计算选中字符串所在的行号，并通过行号得到下一行的第一个字符的起始偏移量
        int curLineNumber = document.getLineNumber(endOffset);
        int nextLineStartOffset = document.getLineStartOffset(curLineNumber + 1);
        //计算字符串的插入位置
        int insertOffset = maxOffset > nextLineStartOffset ? nextLineStartOffset : maxOffset;

        //获取输入字符串&&替换url中的下划线
        String inputText = replaceUnderline(getInputString());
        //获取类名
//        String funName = (inputText.substring(0, inputText.lastIndexOf("("))).trim();
//        className = funName.substring(funName.lastIndexOf(" ") + 1);
        String classFun = document.getImmutableCharSequence().toString();
        className = classFun.substring(classFun.indexOf(" "), classFun.indexOf("{")).replace("class ", "").trim();
        //获取参数集合
        try {
            fieldVars = inputText.substring(inputText.indexOf("(") + 1, inputText.indexOf(")")).split(",");
        } catch (Exception exception) {
            throw new IllegalArgumentException("输入字符串格式错误");
        }

        Runnable runnable = () -> {
            //写build()和reset()方法
            document.insertString(insertOffset, genBuildAndResetFun());
            //写属性设置
            for (String fieldVar : fieldVars) {
                String item = fieldVar.trim();
                String type = item.substring(0, item.lastIndexOf(" "));
                String field = item.substring(item.lastIndexOf(" ") + 1);
                document.insertString(insertOffset, genBuilderSetter(field, type));
            }
            //写Builder类
            document.insertString(insertOffset, genBuilderClass());
            //写文件头
            for (String fieldVar : fieldVars) {
                String item = fieldVar.trim();
                String type = item.substring(0, item.lastIndexOf(" "));
                String field = item.substring(item.lastIndexOf(" ") + 1);
                document.insertString(insertOffset, genFieldItem(field, type));
            }
        };

        WriteCommandAction.runWriteCommandAction(project, runnable);
    }

    //替换下划线
    private String replaceUnderline(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!String.valueOf(chars[i]).equals("_")) {
                if (i > 0) {
                    if (String.valueOf(chars[i - 1]).equals("_")) {
                        stringBuilder.append(String.valueOf(chars[i]).toUpperCase());
                    } else {
                        stringBuilder.append(chars[i]);
                    }
                } else {
                    stringBuilder.append(chars[i]);
                }
            }
        }
        return stringBuilder.toString();
    }

    //写build()和reset()方法
    private String genBuildAndResetFun() {
        String buildFun = "public " + upperClassName() + " build(){ "
                + "\n\t\treturn " + lowerClassName() + ";\n\t}";

        String resetFun = "\n\tpublic void reset(){ "
                + "\n\t\t" + lowerClassName() + " = null;"
                + "\n\t\t" + lowerClassName() + " = new " + upperClassName() + "();\n\t}";
        return "\n\t" + buildFun + "\n" + resetFun + "\n\t}\n";
    }

    //写Builder类
    private String genBuilderClass() {
        return "\n\tclass Builder {" + "\n\n\t"
                + upperClassName() + " " + lowerClassName() + " = new " + upperClassName() + "();\n\t";
    }

    //大写类名
    private String upperClassName() {
        return String.valueOf(className.charAt(0)).toUpperCase() + className.substring(1);
    }

    //小写类名
    private String lowerClassName() {
        return String.valueOf(className.charAt(0)).toLowerCase() + className.substring(1);
    }

    //写Builder构建
    private String genBuilderSetter(String field, String type) {
        if (field == null || (field = field.trim()).equals(""))
            return "";
        String upperField = field;
        char first = field.charAt(0);
        if (first <= 'z' && first >= 'a') {
            upperField = String.valueOf(first).toUpperCase() + field.substring(1);
        }
        String setter = "\tpublic Builder setField(TYPE FIELD){\n\t\t" + lowerClassName() + ".FIELD = FIELD;\n\t" + "\treturn this; \n\t}";
        String mSetter = setter.replaceAll("TYPE", type).replaceAll("Field", upperField).replaceAll("FIELD", field);

        return "\n" + mSetter + "\n";
    }

    //写成员变量
    private String genFieldItem(String field, String type) {
        if (field == null || (field = field.trim()).equals("")) {
            return "";
        }
        String fieldItem = "\tTYPE FIELD;";
        String item = fieldItem.replaceAll("TYPE", type).replaceAll("FIELD", field);
        return item + "\n";
    }

    //插件提示框
    private String getInputString() {
//        String performText = Messages.showInputDialog(project,
//                "url string", "Type in Url String",
//                Messages.getQuestionIcon());
        JFormattedTextField textField = new JFormattedTextField();
        textField.setText("参考格式:\nObservable<Response<Edm>> edm(\n" +
                "            @Query(\"task_id\") String task_id,\n" +
                "            @Query(\"user_id\") String user_id\n" +
                "    );");
        textField.selectAll();
        textField.setSelectedTextColor(Color.BLUE);
        Messages.showTextAreaDialog(textField, "Type in Url String", "");
        String performText = textField.getText();
        return performText.replaceAll("\\@\\w+?\\(\\\"[\\w_\\[\\]]+?\\\"\\)", "");
    }
}
