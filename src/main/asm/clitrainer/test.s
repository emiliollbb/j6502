; == 16K ROM ==
*=$c000

_main:
.(
	LDA #'A'
	STA $8001
	LDA #$0D
	STA $8001
	end: BRA end
.)




; === VECTORS ===
.dsb $fffa-*, $00
.word $0000 ; NMI vector
.word _main ; Reset vector
.word $0000 ; IRQ/BRK vector

