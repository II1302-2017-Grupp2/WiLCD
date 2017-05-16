/*translate([-70, 0, 0]) {
    cube([57, 56, 1]);
}*/

module box() {
    translate([0,0,16]) {
        difference() {
            translate([-2, -15, -16]) {
                cube([101, 99, 21]);
                
                translate([0, 0, 40]) {
                    cube([101, 99, 24]);
                }
            }
            
            translate([15, 15, 20]) {
                cube([72, 50, 40]);
            }
            translate([12.5, 10, 20]) {
                cube([78, 61, 24]);
            }
            
            translate([16.5, 13, 20]) {
                cylinder(40, d = 3, $fn = 20);
            }
            translate([16.5, 67, 20]) {
                cylinder(40, d = 3, $fn = 20);
            }
            translate([85.5, 13, 20]) {
                cylinder(40, d = 3, $fn = 20);
            }
            translate([87.5, 67, 20]) {
                cylinder(40, d = 3, $fn = 20);
            }
            
            translate([81,5,-17]) {
                cylinder(100, d = 2.7, $fn = 20);
                cylinder(14, d = 6, $fn = 6);
                translate([0,0,46]) {
                    cylinder(30, d = 8, $fn = 20);
                }
            }
            translate([5,76,-17]) {
                cylinder(100, d = 2.7, $fn = 20);
                cylinder(14, d = 6, $fn = 6);
                translate([0,0,46]) {
                    cylinder(30, d = 8, $fn = 20);
                }
            }

            cube([88, 81, 10]);
            translate([31,30,-17]) {
                cube([55,51,18]);
                translate([-2,-2,2]) {
                    cube([59, 58, 2]);
                }
            }
            translate([50,-13,-14]) {
                cube([23,35,25]);
            }
            translate([80,49,-10]) {
                cube([16,28,20]);
            }
            translate([0, -13, 0]) {
                cube([40, 20, 20]);
            }
        }
    }
}

/*intersection() {
    box();
    translate([-50, -50, 0]) {
        cube([200, 200, 30]);
    }
}*/

rotate([180, 0, 0]) {
    translate([150, -80, -64]) {
        intersection() {
            box();
            translate([-50, -50, 25]) {
                cube([200, 200, 60]);
            }
        }
    }
}