; == 16K ROM ==
*=$c000

.asc "##gameeeeeeeee##"
; -- main method --
_main:
.(
	; Set video mode
	LDX #$55
	STX $8000
	
	
	end: BRA end
.)


_nmi_int:
.(
    RTI
.)
_irq_int:
.(
    RTI
.)




; === VECTORS ===
.dsb $fffa-*, $00
.word _nmi_int ; NMI vector
.word _main ; Reset vector
.word _irq_int ; IRQ/BRK vector

