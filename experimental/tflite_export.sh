#!/bin/bash

export DATA_DIR=/tmp/chrome/mobilebert/data_cache/
export INIT_CHECKPOINT=./mobilebert
export OUTPUT_DIR=/tmp/chrome/mobilebert/experiment/
export EXPORT_DIR='build'

#export tflite
python3 3rdparty/googleResearch/mobilebert/run_squad.py \
  --use_post_quantization=true \
  --activation_quantization=false \
  --data_dir=${DATA_DIR}  \
  --output_dir=${OUTPUT_DIR} \
  --vocab_file=${INIT_CHECKPOINT}/vocab.txt \
  --bert_config_file=config/uncased_L-24_H-128_B-512_A-4_F-4_OPT.json \
  --train_file=/path/to/squad/train-v1.1.json \
  --export_dir=${EXPORT_DIR}