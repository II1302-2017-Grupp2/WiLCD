use std::{fs, env, vec};
use std::io::Read;
use std::iter::FromIterator;
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

    fn to_c_def(&self, chr: char) -> (String, String) {
        let name = format!("font_{}", u32::from(chr));
        let out = format!("uint8_t {}[] = {};\n", name, self.to_c());
        (name, out)
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

    fn char_to_c(&self, out: &mut String, names: &HashMap<char, String>, chr_num: u8, last: bool) {
        let chr = char::from(chr_num);
        *out += "  ";
        if let Some(ref font_char_name) = names.get(&chr) {
            *out += &font_char_name;
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
        out += "#include \"font.h\"\n\n";
        let mut names = HashMap::new();
        let mut char_pairs = vec::Vec::from_iter(self.chars.iter());
        char_pairs.sort_by_key(|x| x.0);
        for (char_num, font_char) in char_pairs {
            let (name, char_c) = font_char.to_c_def(*char_num);
            names.insert(*char_num, name);
            out += &char_c;
        }
        out += "uint8_t *font[] = {\n";
        for chr_num in 0..255 {
            self.char_to_c(&mut out, &names, chr_num, false);
        }
        self.char_to_c(&mut out, &names, 255, true);
        out += "};";
        out
    }
}

fn main() {
    let mut args = env::args();
    args.next();
    let src_path = args.next().expect("Usage: binfontify <txtfont.txt>");

    let src = {
        let mut buf = String::new();
        fs::File::open(src_path).unwrap().read_to_string(&mut buf).unwrap();
        buf
    };
    let font = Font::from_txtfont(&src);
    println!("{}", font.to_c());
}
