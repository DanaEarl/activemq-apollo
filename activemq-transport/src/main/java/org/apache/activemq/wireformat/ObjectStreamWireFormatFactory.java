package org.apache.activemq.wireformat;

import org.apache.activemq.util.buffer.Buffer;

public class ObjectStreamWireFormatFactory implements WireFormatFactory {

	public WireFormat createWireFormat() {
		return new ObjectStreamWireFormat();
	}	

    public boolean isDiscriminatable() {
        return false;
    }

    public boolean matchesWireformatHeader(Buffer byteSequence) {
        throw new UnsupportedOperationException();
    }

    public int maxWireformatHeaderLength() {
        throw new UnsupportedOperationException();
    }
}
