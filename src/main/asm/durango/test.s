; == 32K ROM ==
*=$8000

_main:
.(
	; Set video mode
    ; [HiRes Invert S1 S0    RGB LED NC NC]
    LDA #$3F
    STA $df80

	LDX #0
	STX $6000
	INX
	STX $6001
	INX
	STX $6002
	INX
	STX $6003
	INX
	STX $6004
	INX
	STX $6005
	INX
	STX $6006
	INX
	STX $6007
	INX
	STX $6008
	INX
	STX $6009
	INX
	STX $600a
	INX
	STX $600b
	INX
	STX $600c
	INX
	STX $6000
	INX
	STX $6000
	INX
	STX $6000
	INX
	STX $6000
	
	end: BRA end
.)




; === VECTORS ===
.dsb $fffa-*, $00
.word $0000 ; NMI vector
.word _main ; Reset vector
.word $0000 ; IRQ/BRK vector

