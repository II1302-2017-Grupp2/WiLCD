difference() {
    union() {
        cube([87, 90, 10]);
        
        translate([-11, 46, 0]) {
            cube([19, 28, 10]);
        }
        
        translate([49, 89, 0]) {
            cube([22, 12, 10]);
        }
    }
    
    translate([-10, 47, 2]) {
        cube([20, 26, 30]);
    }
    translate([50, 88, 2]) {
        cube([20, 12, 10]);
    }
    
    translate([1,1,2]) {
        cube([85, 88, 30]);
    }
    translate([60,45,-10]){
        cube([5,25,30]);
    }
}
