package main;

import org.apfloat.Apfloat;

public class TupleApfloat {
    Apfloat p;
    Apfloat q;
    Apfloat t;
    public TupleApfloat(Apfloat p, Apfloat q, Apfloat t){
        this.p = p;
        this.q = q;
        this.t = t;
    }
    public Apfloat getP(){
        return this.p;
    }
    public Apfloat getQ(){
        return this.q;
    }
    public Apfloat getT(){
        return this.t;
    }
}
