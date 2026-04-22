; == 16K ROM ==
*=$c000

.asc "#game#"
; -- main method --
_main:
.(
	; Set video mode
	LDX #$55
	STA $8000
.)


_init:
.(
    ; Disable interrupts
    SEI
    ; Initialize stack pointer to $01FF
    LDX #$FF
    TXS
    ; Clear decimal mode
    CLD
    ; Enable interrupts
    CLI
    JMP _main
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
.word _init ; Reset vector
.word _irq_int ; IRQ/BRK vector

