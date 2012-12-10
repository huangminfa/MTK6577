#pragma once
#ifndef COM_MEDIATEK_A3M_JNI_COMMON_H
#define COM_MEDIATEK_A3M_JNI_COMMON_H

#include <jni.h> /* for JNI API */
#include <a3m/base_types.h>  /* for base types */
#include <a3m/log.h>         /* for logging */
#include <a3m/noncopyable.h> /* for NonCopyable */

template<typename T> int NELEM(T x)
{
  return ((int) (sizeof(x) / sizeof((x)[0])));
}

/*
 * Register native JNI-callable methods.
 *
 * "className" looks like "java/lang/String".
 */
int registerMethods(JNIEnv* env, const char* className,
                    const JNINativeMethod* gMethods, int numMethods);

void initNativeObjectClass(JNIEnv* env, jclass clazz);
jfieldID getNativeObjectField();
jobject createJavaObject(JNIEnv* env, const char* className, void* native);

inline void setNativeObject(JNIEnv* env, jobject obj, void* native)
{
  env->SetLongField(obj, getNativeObjectField(), reinterpret_cast<jlong>(native));
}

template<typename T>
T* getNativeObject(JNIEnv* env, jobject obj)
{
  return obj ? reinterpret_cast<T*>(env->GetLongField(obj, getNativeObjectField())) : 0;
}

/**
 * Manages a Java global reference.
 * It is essential when programming using the JNI, that any Java object stored
 * outside of the scope of the JNI function call be stored as a global
 * reference.  This is true whether the object was created manually, or passed
 * in from the Java side.  This class automatically increments and
 * decrements the reference count.
 *
 * The jclass type also count as a Java object, but jmethodID and jfieldID
 * don't; thus, fields and methods can be cached for later use without storing
 * a global reference (or even a global reference of the class to which they
 * belong).
 */
template<typename T>
class GlobalRef : a3m::NonCopyable
{
public:
  /**
   * Constructor.
   */
  GlobalRef(JNIEnv* env /**< Java environment */) :
    m_jObject(0)
  {
    if (env->GetJavaVM(&m_vm) < 0)
    {
      A3M_LOG_ERROR("Failed to acquire JavaVM", 0);
      return;
    }
  }

  /**
   * Constructor.
   */
  GlobalRef(
    JNIEnv* env, /**< Java environment */
    T jObject,   /**< Java object to reference */
    A3M_BOOL incRefCount = A3M_TRUE /**< TRUE to increase the reference count */
  ) :
    m_jObject(0)
  {
    if (env->GetJavaVM(&m_vm) < 0)
    {
      A3M_LOG_ERROR("Failed to acquire JavaVM", 0);
      return;
    }

    reset(jObject, incRefCount);
  }

  /**
   * Destructor.
   */
  ~GlobalRef()
  {
    reset();
  }

  /**
   * Sets the Java object referenced.
   */
  void reset(
    T jObject = 0, /**< Java object to reference */
    A3M_BOOL incRefCount = A3M_TRUE  /**< TRUE to increase the reference count */
  )
  {
    JNIEnv* env;
    if (m_vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
    {
      A3M_LOG_ERROR("Failed to acquire JNIEnv", 0);
      return;
    }

    if (m_jObject)
    {
      env->DeleteGlobalRef(m_jObject);
    }

    m_jObject = jObject;

    if (incRefCount)
    {
      m_jObject = static_cast<T>(env->NewGlobalRef(m_jObject));
    }
  }

  /**
   * Returns the Java object referenced.
   * \return Java object
   */
  T get() const
  {
    return m_jObject;
  }

private:
  JavaVM* m_vm; /**< Java VM */
  T m_jObject; /**< Java object referenced */
};

// Utility class to convert a Java string to a C string and handle the memory
// deallocation
class CString : a3m::NonCopyable
{
private:
  JavaVM* mVm;
  GlobalRef<jstring> mJString;
  const char* mCString;

public:
  CString(JNIEnv* env, jstring string) :
    mJString(env, string),
    mCString(0)
  {
    if (env->GetJavaVM(&mVm) < 0)
    {
      A3M_LOG_ERROR("Failed to acquire JavaVM", 0);
      return;
    }

    if (mJString.get())
    {
      mCString = env->GetStringUTFChars(mJString.get(), 0);
    }
  }

  CString(JNIEnv* env, A3M_CHAR8 const* string) :
    mJString(env),
    mCString(0)
  {
    if (env->GetJavaVM(&mVm) < 0)
    {
      A3M_LOG_ERROR("Failed to acquire JavaVM", 0);
      return;
    }

    if (string)
    {
      mJString.reset(env->NewStringUTF(string));
      mCString = env->GetStringUTFChars(mJString.get(), 0);
    }
  }

  ~CString()
  {
    JNIEnv* env;
    if (mVm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
    {
      A3M_LOG_ERROR("Failed to acquire JNIEnv", 0);
      return;
    }

    if (mCString)
    {
      env->ReleaseStringUTFChars(mJString.get(), mCString);
    }
  }

  jstring getJString() const
  {
    return mJString.get();
  }

  const char* getString() const
  {
    return mCString;
  }

  bool isValid() const
  {
    return (mCString != 0);
  }
};

// Utility class to convert a Java byte array to a C byte array and handle
// the memory deallocation
class CByteArray : a3m::NonCopyable
{
private:
  JavaVM* mVm;
  GlobalRef<jbyteArray> mJByteArray;
  jbyte* mCByteArray;
  jint mJMode;

public:
  CByteArray(JNIEnv* env, jbyteArray byteArray) :
    mJByteArray(env, byteArray),
    mCByteArray(0),
    // JNI_ABORT specifies that the array contents should not be copied back
    // into the Java byte array object.
    mJMode(JNI_ABORT)
  {
    if (env->GetJavaVM(&mVm) < 0)
    {
      A3M_LOG_ERROR("Failed to acquire JavaVM", 0);
      return;
    }

    if (mJByteArray.get())
    {
      mCByteArray = env->GetByteArrayElements(mJByteArray.get(), 0);
    }
  }

  CByteArray(JNIEnv* env, jsize size) :
    mJByteArray(env),
    mCByteArray(0),
    // 0 specifies that the array contents should be copied back into the Java
    // byte array object.
    mJMode(0)
  {
    if (env->GetJavaVM(&mVm) < 0)
    {
      A3M_LOG_ERROR("Failed to acquire JavaVM", 0);
      return;
    }
    env->GetJavaVM(&mVm);

    mJByteArray.reset(env->NewByteArray(size));
    mCByteArray = env->GetByteArrayElements(mJByteArray.get(), 0);
  }

  ~CByteArray()
  {
    commit();
  }

  void commit()
  {
    JNIEnv* env;
    if (mVm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
    {
      A3M_LOG_ERROR("Failed to acquire JNIEnv", 0);
      return;
    }

    if (mCByteArray)
    {
      env->ReleaseByteArrayElements(mJByteArray.get(), mCByteArray, mJMode);
      mCByteArray = 0;
    }
  }

  jbyteArray getJByteArray() const
  {
    return mJByteArray.get();
  }

  jbyte* getByteArray()
  {
    return mCByteArray;
  }

  jbyte const* getByteArray() const
  {
    return mCByteArray;
  }

  int getLength() const
  {
    JNIEnv* env;
    if (mVm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
    {
      A3M_LOG_ERROR("Failed to acquire JNIEnv", 0);
      return 0;
    }

    if (isValid())
    {
      return env->GetArrayLength(mJByteArray.get());
    }
    else
    {
      return 0;
    }
  }

  bool isValid() const
  {
    return (mCByteArray != 0);
  }
};

#endif /* COM_MEDIATEK_A3M_JNI_COMMON_H */

