cmake_minimum_required(VERSION 3.10.2)

project("aidlcppservice")

add_library (
        native-lib
        SHARED
        native-lib.cpp
        MyService.cpp
        aidl/com/example/IMyService.cpp
)

target_include_directories (
        native-lib
        PRIVATE
        includes
)

find_library (
        log-lib
        log
)

find_library (
        binder_ndk-lib
        binder_ndk
)

target_link_libraries (
        native-lib
        ${log-lib}
        ${binder_ndk-lib}
)
