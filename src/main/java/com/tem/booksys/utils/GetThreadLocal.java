package com.tem.booksys.utils;

import java.util.Map;

public class GetThreadLocal {

    Map<String,Object> map = ThreadLocalUtil.get();
    public Integer getNowThreadLocalId(){

        return (Integer) map.get("id");
    }
    public String getNowThreadLocalUsername(){
        return (String) map.get("username");
    }
}
