# FFDec Frequently asked questions (FAQ)

## "Invalid SWF file, wrong signature" on opening file with .swf extension, what's that?
This message means that you are opening file which is not really the SWF. FFDec can process only valid SWF files - those which can be played in Flash player. SWF file extension is not enough - also file structure must be compliant
with SWF standard - it needs to start with FWS, CWS or ZWS bytes. If you are 100% sure there is something SWF related inside your file, then it probably needs some kind of
decryption/unpacking routine first. Go where you got the file and search for an unpacking routine (loader), maybe you find something. FFDec cannot help you with this since decryption
routine can be literally anything.

## Can I add new scripts? Or classes?
No, this feature is not implemented, sorry. Editing is limited to existing scripts or classes.

## Direct editation of ActionScript gives me an error, what can I do?
Direct editation of AS is experimental and in most cases not even working - it might damage your SWF file. We won't fix that, sorry.

## Can you implement new feature for me?
No, we no longer work on the decompiler. But decompiler is opensource, you can implement feature yourself.

## But there are bugs, you will surely fix that
No, sorry, we no longer fix any bugs. Ask somebody else.

## Is there a documentation for FFDec library?
No, there isn't one.

## What's that thing with § character in scripts?
A) Variable identifiers which have invalid characters in their name are displayed in the form `§name§` (for example `§s-r/rg§` ).
This allows easier direct editation.

B) Also, there exist few special instructions/functions with prefix §§:
- `§§push(item)` - pushes item on stack
- `§§pop()` - pops from stack
- `§§dup()` - duplicates value on stack
- `§§goto(label)` - jump to address
- `§§constant(number)` - unresolved constant (unknown constantpool)

These functions usually pop out when you try to decompile some obfuscated / unstructured code.
You can try Settings/Automatic deobfuscation option for handle some kinds of this code properly.
