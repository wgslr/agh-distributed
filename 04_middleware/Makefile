GRPC_JAVA_PATH=/usr/local/bin/protoc-gen-grpc-java-0.15.0-linux-x86_64.exe
PROTOC_FLAGS=--plugin=protoc-gen-grpc-java=$(GRPC_JAVA_PATH)

all: clean slice proto

.PHONY: slice proto clean

clean:
	-rm -rf {bank,currencytracker}/generated

slice: api/*.ice
	mkdir -p bank/generated
	slice2java --output-dir bank/generated $^

proto: api/*.proto
	mkdir -p {currencytracker,bank}/generated
	protoc $(PROTOC_FLAGS) --java_out=currencytracker/generated --grpc-java_out=currencytracker/generated  $^
	protoc $(PROTOC_FLAGS) --java_out=bank/generated --grpc-java_out=bank/generated $^
