package SimpleValid {
    private import ScalarValues::*;

    part def Vehicle {
        attribute mass : Real;
    }

    part vehicle : Vehicle {
        attribute redefines mass = 1500.0;
    }
}
