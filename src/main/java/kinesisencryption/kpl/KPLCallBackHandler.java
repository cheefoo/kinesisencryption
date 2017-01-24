/**
 * 
 */
package kinesisencryption.kpl;

import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.util.concurrent.FutureCallback;

/**
 * @author Administrator
 *
 */
public class KPLCallBackHandler implements FutureCallback {

	/* (non-Javadoc)
	 * @see com.google.common.util.concurrent.FutureCallback#onSuccess(java.lang.Object)
	 */
	
	/* (non-Javadoc)
	 * @see com.google.common.util.concurrent.FutureCallback#onFailure(java.lang.Throwable)
	 */
	@Override
	public void onFailure(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(Object result) {
		// TODO Auto-generated method stub
		
	}

}
