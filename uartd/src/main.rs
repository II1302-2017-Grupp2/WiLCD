use std::{fs, net, thread};
use std::io::{Read, Write};

fn main() {
    let listener = net::TcpListener::bind("0.0.0.0:9797").unwrap();
    for sock in listener.incoming() {
        let sock = sock.unwrap();
        let sock_read = sock.try_clone().unwrap();
        let mut sock_write = sock.try_clone().unwrap();
        let path = "/dev/ttyAMA0";
        let thread_file_to_sock = thread::spawn(move || {
            let file = fs::File::open(path).unwrap();
            for b in file.bytes() {
                sock_write.write(&[b.unwrap()]).unwrap();
            }
        });
        let thread_sock_to_file = thread::spawn(move || {
            let mut file = fs::File::create(path).unwrap();
            for b in sock_read.bytes() {
                file.write(&[b.unwrap()]).unwrap();
            }
        });
        thread_file_to_sock.join().unwrap();
        thread_sock_to_file.join().unwrap();
    }
}
