#!/bin/bash

TASK_NAME=cola
python3 3rdparty/googleResearch/mobilebert/run_classifier.py \
	--task_name=${TASK_NAME} \
	--vocab_file=mobilebert/vocab.txt \
	--bert_config_file=3rdparty/googleResearch/mobilebert/config/uncased_L-24_H-128_B-512_A-4_F-4_OPT.json \
	--output_dir=/tmp/chrome/mobilebert/experiment/ \
	--do_eval=true \
	--data_dir=/tmp/chrome/mobilebert/data_cache/