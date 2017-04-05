use std::fs;
use std::io::Read;
use std::collections::HashMap;
use std::str::Lines;

#[derive(Debug)]
struct Character {
    bytes: Vec<u8>
}

#[derive(Debug)]
struct Font {
    chars: HashMap<char, Character>
}

impl Character {
    fn from_txtfont_lines(txtchar: &mut Lines) -> Character {
        let mut font_char = Character {
            bytes: vec![]
        };
        for i in 0..8 {
            let line = match txtchar.next() {
                Some(x) => x,
                None => break
            };
            for (j, chr) in line.chars().enumerate() {
                while j >= font_char.bytes.len() {
                    font_char.bytes.push(0);
                }
                font_char.bytes[j] |= match chr {
                    ' ' => 0,
                    '.' => 1,
                    _ => panic!("Invalid font character '{}', ' ' or '.' allowed", chr)
                } << i;
            }
        };
        assert!(font_char.bytes.len() <= 255);
        font_char
    }

    fn to_c(&self) -> String {
        let mut out = String::new();
        out += "{ ";
        out += self.bytes.len().to_string().as_ref();
        for b in self.bytes.iter() {
            out += format!(", 0x{:X}", b).as_ref();
        }
        out += "}";
        out
    }
}

impl Font {
    fn from_txtfont(txtfont: &str) -> Font {
        let mut font = Font {
            chars: HashMap::new()
        };

        let mut lines = txtfont.lines();
        loop {
            if let Some(line) = lines.next() {
                let mut chars = line.chars();
                assert_eq!(Some(':'), chars.next());
                let chr = chars.next().unwrap();
                assert_eq!(None, chars.next());
                font.chars.insert(chr, Character::from_txtfont_lines(&mut lines));
            } else {
                break;
            }
        }
        font
    }

    fn char_to_c(&self, out: &mut String, chr_num: u8, last: bool) {
        let chr = char::from(chr_num);
        *out += "  ";
        if let Some(font_char) = self.chars.get(&chr) {
            *out += font_char.to_c().as_ref();
        } else {
            *out += "NULL";
        }
        if !last {
            *out += ",";
        }
        *out += format!("\t\t/* {:?} ({}) */\n", chr, chr_num).as_ref();
    }

    fn to_c(&self) -> String {
        let mut out = String::new();
        out += "uint8_t font[][] = {\n";
        for chr_num in 0..255 {
            self.char_to_c(&mut out, chr_num, false);
        }
        self.char_to_c(&mut out, 255, true);
        out += "};";
        out
    }
}

fn main() {
    let src_path = "WiLCD-Sans.txt";
    let src = {
        let mut buf = String::new();
        fs::File::open(src_path).unwrap().read_to_string(&mut buf).unwrap();
        buf
    };
    let font = Font::from_txtfont(&src);
    println!("{}", font.to_c());
}
