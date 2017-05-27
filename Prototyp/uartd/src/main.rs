use std::{net, env, thread, fs, io, process};
use std::io::{Read, Write};
use std::str::FromStr;

enum Mode {
    Client,
    Server,
}

impl Mode {
    fn from_str(str: &str) -> io::Result<Mode> {
        match str {
            "client" => Ok(Mode::Client),
            "server" => Ok(Mode::Server),
            _ => Err(io::Error::new(io::ErrorKind::InvalidInput, "Invalid mode")),
        }
    }
}

struct Args {
    mode: Mode,
    tty_path: String,
    port: u16,
}

impl Args {
    fn read() -> io::Result<Args> {
        let mut args = env::args();
        args.next();
        let mode = Mode::from_str(args.next().unwrap_or("".to_string()).as_ref())?;
        let tty_path = args.next().unwrap_or("/dev/ttyUSB0".to_owned());
        let port = u16::from_str(args.next().unwrap_or("9797".to_owned()).as_ref()).or_else(|err| Err(io::Error::new(io::ErrorKind::InvalidInput, err)))?;
        if args.next().is_some() {
            return Err(io::Error::new(io::ErrorKind::InvalidInput, "Too many arguments"));
        }
        Ok(Args {
            mode: mode,
            tty_path: tty_path,
            port: port,
        })
    }
}

pub fn proxy_tty_sock(sock: net::TcpStream, tty_path: &str) {
    let sock_read = sock.try_clone().unwrap();
    let mut sock_write = sock.try_clone().unwrap();
    let tty_path_owned = tty_path.to_owned();
    let thread_file_to_sock = thread::spawn(move || {
        let file = fs::File::open(tty_path_owned.clone()).unwrap();
        for b in file.bytes() {
            sock_write.write(&[b.unwrap()]).unwrap();
        }
    });
    let tty_path_owned = tty_path.to_owned();
    let thread_sock_to_file = thread::spawn(move || {
        let mut file = fs::File::create(tty_path_owned).unwrap();
        for b in sock_read.bytes() {
            file.write(&[b.unwrap()]).unwrap();
        }
    });
    thread_file_to_sock.join().unwrap();
    thread_sock_to_file.join().unwrap();
}

fn main() {
    let args = Args::read().unwrap_or_else(|err| {
        println!("Failed to read arguments: {}", err);
        println!("Usage: uartd <client|server> [tty] [port]");
        process::exit(1);
    });

    let sock_addr = format!("0.0.0.0:{}", args.port);

    match args.mode {
        Mode::Server => {
            let listener = net::TcpListener::bind(sock_addr).unwrap();
            for sock in listener.incoming() {
                proxy_tty_sock(sock.unwrap(), &args.tty_path);
            }
        }
        Mode::Client => {
            let sock = net::TcpStream::connect(sock_addr).unwrap();
            proxy_tty_sock(sock, &args.tty_path);
        }
    }
}
