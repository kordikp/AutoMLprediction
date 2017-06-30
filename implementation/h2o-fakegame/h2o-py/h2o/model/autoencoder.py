import h2o
from .model_base import ModelBase

class H2OAutoEncoderModel(ModelBase):

  def anomaly(self,test_data,per_feature=False):
    """Obtain the reconstruction error for the input test_data.

    Parameters
    ----------
      test_data : H2OFrame
        The dataset upon which the reconstruction error is computed.

      per_feature : bool
        Whether to return the square reconstruction error per feature. Otherwise, return
        the mean square error.

    Returns
    -------
      Return the reconstruction error.
    """
    if test_data is None or test_data.nrow == 0: raise ValueError("Must specify test data")
    j = h2o.H2OConnection.post_json("Predictions/models/" + self.model_id + "/frames/" + test_data.frame_id, reconstruction_error=True, reconstruction_error_per_feature=per_feature)
    return h2o.get_frame(j["model_metrics"][0]["predictions"]["frame_id"]["name"])
