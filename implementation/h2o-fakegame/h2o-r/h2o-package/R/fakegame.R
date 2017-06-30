#' FAKEGAME
#'
#' Builds a FAKEGAME model
#'
#' @export

h2o.fakegame <- function(x, y, training_frame, model_id, model_config){
    # Required maps for different names params, including deprecated params
    .fakegame.map <- c("x" = "ignored_columns",  "y" = "response_column")

    # Training_frame may be a key or an H2OFrame object
    if (!is.H2OFrame(training_frame))
    tryCatch(training_frame <- h2o.getFrame(training_frame),
    error = function(err) {
        stop("argument \"training_frame\" must be a valid H2OFrame or key")
    })
    if (!is.null(validation_frame)) {
        if (!is.H2OFrame(validation_frame))
        tryCatch(validation_frame <- h2o.getFrame(validation_frame),
        error = function(err) {
            stop("argument \"validation_frame\" must be a valid H2OFrame or key")
        })
    }

    # Parameter list to send to model builder
    parms <- list()
    parms$training_frame <- training_frame
    args <- .verify_dataxy(training_frame, x, y)

    parms$ignored_columns <- args$x_ignore
    parms$response_column <- args$y
    if (!missing(model_id))
    parms$model_id <- model_id
    if(!missing(model_config))
    parms$model_config <- model_config

    .h2o.modelJob('fakegame', parms)
}

