#!/bin/bash

set -ev

dry_run=false
while (( $# > 0 )); do
    case "$1" in
    --dry-run)
        dry_run=true
        ;;
    *)
        ;;
    esac
    shift
done

echo "dry_run: ${dry_run}"

flag="-U -B"

if [ "${TRAVIS_BRANCH}" = "master" ]; then
    if [ "${TRAVIS_PULL_REQUEST}" = "false" ]; then
        # build of master, run tests and push artifacts
        maven_goal=deploy
        skip_test=false
    else
        # build of pull reqest into master, run tests
        maven_goal=install
        skip_test=false
    fi
elif [[ "${TRAVIS_BRANCH}" =~ ^release ]]; then
    if [[ "${TRAVIS_COMMIT_MESSAGE}" =~ TRIGGER_RELEASE ]];then
        maven_goal="release:prepare release:perform"
        skip_test=false
        # flag="${flag} -DreleaseVersion=4.18.0 -DdevelopmentVersion=4.18.1-SNAPSHOT"
        git checkout ${TRAVIS_BRANCH}
    else
        maven_goal=install
        skip_test=false
    fi
elif [[ "${TRAVIS_TAG}" =~ v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        maven_goal=deploy
        skip_test=false
else
    # build of a branch except master, build only
    maven_goal=install
    skip_test=false
fi

echo "maven_goal: ${maven_goal}"
echo "skip_test: ${skip_test}"
echo "flag: ${flag}"

if [[ ${dry_run} == "false" ]];then
    mvn clean ${maven_goal} ${flag} -f EclipseTooling/pom.xml -s build/maven-settings.xml -Dmaven.test.skip=${skip_test} -Dartifactory.username=${ARTIFACTORY_USERNAME} -Dartifactory.password=${ARTIFACTORY_PASSWORD} -Dghe.username=${GHE_USERNAME} -Dghe.accesstoken=${GHE_ACCESSTOKEN}
fi
