package com.os.services.interceptor.domain;


public class Pages
{

    private int startPage;
    private int endPage;
    
    
    public Pages(int startPage, int endPage)
    {
        this.startPage = startPage;
        this.endPage = endPage;
    }


    public int getStartPage()
    {
        return startPage;
    }


    public int getEndPage()
    {
        return endPage;
    }
    
}
