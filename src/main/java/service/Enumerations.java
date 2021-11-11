/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

/**
 *
 * @author ronald.langat
 */
public class Enumerations {

   public enum STATE {
        IDLE, LOADING, LOADED, DELIVERING, DELIVERED, RETURNING
    }

  public  enum MODEL {
        Lightweight, Middleweight, Cruiserweight, Heavyweight;
    }
}
