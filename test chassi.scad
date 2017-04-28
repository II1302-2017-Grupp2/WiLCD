difference() {
    union() {
        cube([89, 84, 10]);
        
        translate([-12, 40, 0]) {
            cube([20, 30, 10]);
        }
        
        translate([48, 82, 0]) {
            cube([24, 12, 10]);
        }
    }
    
    translate([-10, 42, 3]) {
        cube([20, 26, 30]);
    }
    translate([50, 80, 3]) {
        cube([20, 12, 10]);
    }
    
    translate([2,2,3]) {
        cube([85, 80, 30]);
    }
    translate([60,47,-10]){
        cube([5,15,30]);
    }
}