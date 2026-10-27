l_lcdd=$FFBD
l_lcdc=$FFBA
l_pad=$FFB7

; == 32K ROM ==
*=$4000

; ***********************
; *** standard header ***
; ***********************
rom_start:
; header ID
	.byt	0				; [0]=NUL, first magic number
	.asc	"pZ"			; downloadable Pocket format (for Zacatecas/Chihuahua)
	.word	rom_start		; load address
	.word	reset			; execution address
	.byt	13				; [7]=NEWLINE, second magic number
; filename
	.asc	"Hello Zacatecas", 0 ; C-string with filename @ [8], max 220 chars
	.asc	"Typical first example for learning to code", 0 ; optional C-string with comment after filename, filename+comment up to 220 chars

; advance to end of header
	.dsb	rom_start + $E6 - *, $FF

; NEW library commit (user field 2)
	.asc	"$$$$$$$$"
; NEW main commit (user field 1)
	.asc	"$$$$$$$$"
; NEW coded version number
	.word	$1008			; 1.0a8		%vvvvrrrrsshhbbbb, where revision = %hhrrrr, ss = %00 (alpha), %01 (beta), %10 (RC), %11 (final)

; date & time in MS-DOS format at byte 248 ($F8)
	.word	$A320			; time, 20.25		1010 0-011 001-0 0000
	.word	$5CFC			; date, 2026/7/28	0101 110-0 111-1 1100
; filesize in top 32 bits (@ $FC) now including header ** must be EVEN number of pages because of 512-byte sectors
	.word	$4000 ; filesize (rom_end is actually $10000)
	.word	0 ;					; 64K space does not use upper 16 bits, [255]=NUL may be third magic number

reset:
; *******************
; *** actual code ***
; *******************
BRK


.dsb $8000-*, $55
