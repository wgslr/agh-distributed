all: clean slice proto

.PHONY: slice proto clean

clean:
	-rm -rf {bank,currencytracker}/generated

slice: api/*.ice
	mkdir -p bank/generated
	slice2java --output-dir bank/generated $^

proto: api/*.proto
	mkdir -p {currencytracker,bank}/generated
	protoc --java_out=currencytracker/generated --java_out=bank/generated $^
