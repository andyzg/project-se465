
$(TARGET)_3_65.out: $(TARGET).bc
	../pipair $< >$@

$(TARGET)_10_80.out: $(TARGET).bc
	../pipair $< 10 80 >$@

outputs: $(TARGET)_3_65.out $(TARGET)_10_80.out

