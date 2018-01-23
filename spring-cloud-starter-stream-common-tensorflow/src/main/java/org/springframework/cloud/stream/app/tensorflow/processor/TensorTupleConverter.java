/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.tensorflow.processor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.tensorflow.DataType;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;

/**
 * Utility that helps to covert {@link Tensor} to {@link Tuple} and in reverse.
 * @author Christian Tzolov
 */
public class TensorTupleConverter {

	public static final String TF_DATA_TYPE = "type";

	public static final String TF_SHAPE = "shape";

	public static final String TF_VALUE = "value";

	public static Tuple toTuple(Tensor tensor) {
		ByteBuffer buffer = ByteBuffer.allocate(tensor.numBytes());
		tensor.writeTo(buffer);

		// Retrieve all bytes in the buffer
		buffer.clear();
		byte[] bytes = new byte[buffer.capacity()];

		buffer.get(bytes, 0, bytes.length);

		return TupleBuilder.tuple()
				.put(TF_DATA_TYPE, tensor.dataType().name())
				.put(TF_SHAPE, tensor.shape())
				.put(TF_VALUE, bytes)
				.build();
	}

	public static Tensor toTensor(Tuple tuple) {
		DataType dataType = DataType.valueOf(tuple.getString(TF_DATA_TYPE));
		long[] shape = (long[]) tuple.getValue(TF_SHAPE);
		byte[] bytes = (byte[]) tuple.getValue(TF_VALUE);

		return Tensor.create(dataTypeToClass(dataType), shape, ByteBuffer.wrap(bytes));
	}

	private static final Map<DataType, Class<?>> typeToClassMap = new HashMap<>();

	static {
		typeToClassMap.put(DataType.FLOAT, Float.class);
		typeToClassMap.put(DataType.DOUBLE, Double.class);
		typeToClassMap.put(DataType.INT32, Integer.class);
		typeToClassMap.put(DataType.UINT8, UInt8.class);
		typeToClassMap.put(DataType.INT64, Long.class);
		typeToClassMap.put(DataType.BOOL, Boolean.class);
		typeToClassMap.put(DataType.STRING, String.class);
	}

	private static Class<?> dataTypeToClass(DataType dataType) {
		Class<?> clazz = typeToClassMap.get(dataType);
		if (clazz == null) {
			throw new IllegalArgumentException("No class found for dataType: " + dataType);
		}
		return clazz;
	}

}