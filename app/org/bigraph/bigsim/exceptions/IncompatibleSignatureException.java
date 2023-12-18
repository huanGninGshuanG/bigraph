package org.bigraph.bigsim.exceptions;


import org.bigraph.bigsim.model.component.Control;
import org.bigraph.bigsim.model.component.Signature;

import java.util.*;

public class IncompatibleSignatureException extends RuntimeException {

	private static final long serialVersionUID = -2623617487911027928L;

	List<Signature> sigs;

	public IncompatibleSignatureException(Collection<? extends Signature> signatures) {
		super();
		List<Signature> sigs = new ArrayList<>(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	public IncompatibleSignatureException(String message, Collection<? extends Signature> signatures) {
		super(message);
		List<Signature> sigs = new ArrayList<>(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	@SafeVarargs
	public IncompatibleSignatureException(Signature... signatures) {
		List<Signature> sigs = Arrays.asList(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	@SafeVarargs
	public IncompatibleSignatureException(String message, Signature... signatures) {
		super(message);
		List<Signature> sigs = Arrays.asList(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	public List<Signature> getClashingSignatures() {
		return this.sigs;
	}

}
