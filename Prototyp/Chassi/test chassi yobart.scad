difference() {
    cube([74,90,6]);
    
    translate([1,1,1]) {
        cube([72,88,10]);
    }
    translate([12, 35, -2]) {
        cube([5, 16, 10]);
    }
}

translate([0,0,5]) {
    cube([74, 3, 1]);
}