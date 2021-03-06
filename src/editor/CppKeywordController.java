package editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Eyup Sen
 */
public final class CppKeywordController {
    public static final String colorTagBegin = "<font color=\"#b31a1a\">";
    public static final String colorTagEnd = "</font>";
    
    public static String findKeywords(String text){
        int flag=-1;
        
        for(int i=0;i<text.length();++i){
            if(text.charAt(i)=='>')
                flag=i+1;
            else if(flag!=-1 && text.charAt(i)=='<'){                
                if(i != flag){
                    String subStr = text.substring(flag,i);
                    for(int j=0; j<Keywords.keywords.length;++j){
                        String highlightedStr = highlightKeywords(subStr,Keywords.keywords[j]);
                        if(!highlightedStr.equals(subStr)){
                            text = text.substring(0,flag)+highlightedStr+text.substring(i,text.length());
                            subStr = highlightedStr;
                            i = flag+subStr.length();
                        }
                    }
                }
            }
        
        }    
      return text;
    }
  
    public static String highlightKeywords(String substr,String keyword){
        int index = substr.indexOf(keyword);
        if(index == -1)
            return substr;
        return substr.substring(0,index)+colorTagBegin+keyword+colorTagEnd+highlightKeywords(substr.substring(index+keyword.length(),substr.length()),keyword);
    }

    public static String unHighlightKeywords(String txt){
        int lengthTagBegin = colorTagBegin.length();
        int lengthTagEnd = colorTagEnd.length();
        
        for(int i=0;i<txt.length();++i){
            if(txt.length() - i >= lengthTagBegin+lengthTagEnd && colorTagBegin.equals(txt.substring(i, i+lengthTagBegin))){
                for(int j=0;j<Keywords.keywords.length;++j){
                    if(txt.length() - i >= lengthTagEnd+Keywords.keywords[j].length()+lengthTagBegin && (Keywords.keywords[j]+colorTagEnd).equals(txt.substring(i+lengthTagBegin, i+lengthTagBegin+Keywords.keywords[j].length()+lengthTagEnd))){
                        txt = txt.substring(0, i) + Keywords.keywords[j] + txt.substring(i+lengthTagBegin+Keywords.keywords[j].length()+lengthTagEnd, txt.length());
                        i = i + Keywords.keywords[j].length()-1;
                        break;
                    }
                }
            }
        }
    
        return txt;
    }
    
    /**
     * Strips htmlText.
     * may be useful later.
     */
    public static String stripHTMLTags(String htmlText) {

        Pattern pattern = Pattern.compile("<[^>]*>");
        Matcher matcher = pattern.matcher(htmlText);
        final StringBuffer sb = new StringBuffer(htmlText.length());
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return (sb.toString().trim());
    }
}
