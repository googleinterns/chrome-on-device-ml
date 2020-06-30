import wget
import argparse
import os
import tensorflow as tf
import tarfile
import numpy as np

MODEL_DIR = "/tmp/chrome/mobilebert"
MODEL_URL = "https://storage.googleapis.com/cloud-tpu-checkpoints/mobilebert/mobilebert_squad_savedmodels.tar.gz"
OUTPUT_DIR = "../android/app/src/main/assets"

def prepare_model(opts):
  #download the model
  if not os.path.exists(MODEL_DIR):
    os.makedirs(MODEL_DIR)
  saved_model_tar = os.path.join(MODEL_DIR, 'mobilebert_squad_saved.tar.gz')
  
  if not os.path.exists(saved_model_tar):
    wget.download(MODEL_URL, saved_model_tar)

  #extract
  tar_file = tarfile.open(saved_model_tar)
  tar_file.extractall(path=MODEL_DIR)

  #convert to tflite
  model_path = os.path.join(MODEL_DIR, "mobilebert_squad_savedmodels/quant_saved_model")
  print(model_path)
  converter = tf.lite.TFLiteConverter.from_saved_model(model_path)
  tflite_model = converter.convert()
  with open(os.path.join(opts.out_dir, "text_mobilebert.tflite"), "wb") as f:
    f.write(tflite_model)

# def evaluate(opts):
#   print("evaluation")
#   interpreter = tf.lite.Interpreter(model_path=os.path.join(OUTPUT_DIR, "text_mobilebert.tflite"))
#   interpreter.allocate_tensors()
#   input_details = interpreter.get_input_details()
#   output_details = interpreter.get_output_details()
#   print(input_details)
#   print(output_details)

#   input_shape = input_details[0]['shape']
#   input_data = np.array(np.random.random_sample(input_shape), dtype=input_details[0]['dtype'])
#   interpreter.set_tensor(input_details[0]['index'], input_data)

#   interpreter.invoke()

#   output_data = interpreter.get_tensor(output_details[0]['index'])
#   print(output_data)

def evaluate_mobilebert(opts):
  from mobilebert import run_classifier
  task_name = "cola"
  vocab_file = "./data/vocab.txt"
  config = "3rdparty/googleResearch/mobilebert/config/uncased_L-24_H-128_B-512_A-4_F-4_OPT.json"
  output = "./build"
  data_dir = "/tmp/chrome/mobilebert/data_cache/"
  eval = "True"
  cmd = f'python3 -m run_classifier --task_name {task_name}\
    --vocab_file {vocab_file}\
    --bert_config_file {config}\
    --output_dir {output}\
    --do_eval {eval}\
    --data_dir {data_dir}\
    '
  os.system(cmd)

if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument('--out-dir', help="Output directory of model files", default=OUTPUT_DIR)
  parser.add_argument('--prepare', help="Prepare the TFLite model", action="store_true")
  parser.add_argument('--evaluate', help="Evaluate TFLite model against dataset", action="store_true")
  opts = parser.parse_args()
  
  if opts.prepare:
    prepare_model(opts)

  if opts.evaluate:
    evaluate_mobilebert(opts)