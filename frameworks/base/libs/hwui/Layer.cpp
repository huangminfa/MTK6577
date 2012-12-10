#include "Layer.h"
#include "Caches.h"

namespace android {
namespace uirenderer {

#if DEBUG_ERROR_CHECK
void Layer::addLayer() {
    mTid = gettid();
    mTimestamp = systemTime(SYSTEM_TIME_MONOTONIC);
    Caches::getInstance().addLayer(this);
}

void Layer::removeLayer() {
    Caches::getInstance().removeLayer(this);
}
#endif

}; // namespace uirenderer
}; // namespace android
