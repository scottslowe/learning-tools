package main

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/pem"
	"fmt"
	"log"

	"golang.org/x/crypto/ssh"
)

func main() {
	// Generate a 4096-bit RSA private key
	privateKey, err := rsa.GenerateKey(rand.Reader, 4096)
	if err != nil {
		log.Printf("error encountered: %s", err.Error())
	}
	privateKeyDer := x509.MarshalPKCS1PrivateKey(privateKey)
	privateKeyBlock := pem.Block{
		Type:    "RSA PRIVATE KEY",
		Headers: nil,
		Bytes:   privateKeyDer,
	}
	privateKeyPem := string(pem.EncodeToMemory(&privateKeyBlock))
	fmt.Println(string(privateKeyPem))

	// Generate a matching public key
	publicKey := privateKey.PublicKey
	publicKeyDer, err := x509.MarshalPKIXPublicKey(&publicKey)
	if err != nil {
		log.Printf("error encountered: %s", err.Error())
	}
	publicKeyBlock := pem.Block{
		Type:    "PUBLIC KEY",
		Headers: nil,
		Bytes:   publicKeyDer,
	}
	publicKeyPem := string(pem.EncodeToMemory(&publicKeyBlock))
	fmt.Println(string(publicKeyPem))

	// Generate the public key in OpenSSH authorized_keys format
	sshPubKey, err := ssh.NewPublicKey(&publicKey)
	if err != nil {
		log.Printf("error encountered: %s", err.Error())
	}
	fmt.Println(string(ssh.MarshalAuthorizedKey(sshPubKey)))
}
