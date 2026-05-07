; == 16K ROM ==
*=$c000

_main:
.(
	LDA #'A'
	LDX #0
	
	loop:
	STA $8001
	INX
	CPX #10
	BCC loop
	
	LDA #$0D
	STA $8001
	end: BRA end
.)




; === VECTORS ===
.dsb $fffa-*, $00
.word $0000 ; NMI vector
.word _main ; Reset vector
.word $0000 ; IRQ/BRK vector

