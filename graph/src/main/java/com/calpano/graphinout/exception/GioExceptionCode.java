/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calpano.graphinout.exception;

/**
 *
 * @author rbaba
 */
public interface GioExceptionCode {
    public String getErrorCode() ;

    public String getErrorMessage() ;

    public ErrorPriority getPriority() ;
    
    public ErrorProvider getProvider();
}
