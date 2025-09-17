package my.foodon.pizzamania.models;

public class Driver {
    public String did;      // driver id
    public String dname;    // driver name
    public String dplate;   // vehicle plate
    public String dtel;     // phone
    public String dbranch;  // branch key (e.g., colombo, galle)

    public Driver() {}

    public Driver(String did, String dname, String dplate, String dtel) {
        this.did = did;
        this.dname = dname;
        this.dplate = dplate;
        this.dtel = dtel;
    }
}


