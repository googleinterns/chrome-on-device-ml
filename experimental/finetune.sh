#!/bin/bash

MOBILEBERT_DIR=3rdparty/googleResearch/mobilebert

# download squad files
mkdir squad
wget -P squad https://rajpurkar.github.io/SQuAD-explorer/dataset/train-v1.1.json
wget -P squad https://rajpurkar.github.io/SQuAD-explorer/dataset/dev-v1.1.json

#download pre-trained weights
wget -N https://storage.googleapis.com/cloud-tpu-checkpoints/mobilebert/uncased_L-24_H-128_B-512_A-4_F-4_OPT.tar.gz
tar -xvf uncased_L-24_H-128_B-512_A-4_F-4_OPT.tar.gz

export DATA_DIR=/tmp/chrome/mobilebert/data_cache/
export INIT_CHECKPOINT=./mobilebert
export OUTPUT_DIR=/tmp/chrome/mobilebert/experiment/

#finetune
python3 ${MOBILEBERT_DIR}/run_squad.py \
  --bert_config_file=${MOBILEBERT_DIR}/config/uncased_L-24_H-128_B-512_A-4_F-4_OPT.json \
  --data_dir=${DATA_DIR} \
  --do_lower_case \
  --do_predict \
  --do_train \
  --doc_stride=128 \
  --init_checkpoint=${INIT_CHECKPOINT}/mobilebert_variables.ckpt.index \
  --learning_rate=4e-05 \
  --max_answer_length=30 \
  --max_query_length=64 \
  --max_seq_length=384 \
  --n_best_size=20 \
  --num_train_epochs=5 \
  --output_dir=${OUTPUT_DIR} \
  --predict_file=squad/dev-v1.1.json \
  --train_batch_size=32 \
  --train_file=squad/train-v1.1.json \
  --vocab_file=${INIT_CHECKPOINT}/vocab.txt \
  --warmup_proportion=0.1